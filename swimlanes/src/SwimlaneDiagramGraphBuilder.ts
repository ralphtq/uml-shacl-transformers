import SwimlaneDiagramData from "./SwimlaneDiagramData";
import {
  DefaultLabelStyle,
  IGraph,
  INode,
  NodeStyleStripeStyleAdapter,
  Rect,
  ShapeNodeStyle,
  Table,
  TableNodeStyle,
} from "yfiles";
import SwimlaneDiagramNode from "./SwimlaneDiagramNode";
import SwimlaneDiagramEdge from "./SwimlaneDiagramEdge";
import SwimlaneDiagramLane from "./SwimlaneDiagramLane";

export default abstract class SwimlaneDiagramGraphBuilder {
  protected readonly alternativeLaneStripeStyle =
    new NodeStyleStripeStyleAdapter(
      new ShapeNodeStyle({
        fill: "#abc8e2",
        stroke: "black",
        shape: "rectangle",
      })
    );

  protected readonly labelStyle = new DefaultLabelStyle({
    backgroundFill: "#e0e0e0",
    backgroundStroke: "black",
    verticalTextAlignment: "center",
    horizontalTextAlignment: "center",
    textSize: 16,
    insets: [3, 5, 3, 5],
  });

  protected readonly laneStripeStyle = new NodeStyleStripeStyleAdapter(
    new ShapeNodeStyle({
      fill: "#c4d7ed",
      stroke: "black",
      shape: "rectangle",
    })
  );

  // Store lanes and nodes references.
  protected readonly laneIndicesById: {[index: string]: number};
  // one column with several rows (i.e. horizontal swimlanes).
  protected readonly table = new Table();

  private readonly graphNodesById: {[index: string]: INode} = {};

  protected constructor(
    private readonly data: SwimlaneDiagramData,
    protected readonly graph: IGraph
  ) {
    this.laneIndicesById = data.lanes.reduce((map, lane, laneIndex) => {
      map[lane.id] = laneIndex;
      return map;
    }, {} as {[index: string]: number});
  }

  build(): void {
    this.createLanes(this.data.lanes);
    const tableGroupNode = this.createTableGroupNode();
    this.createGraphNodes(this.data.nodes, tableGroupNode);
    this.createGraphEdges(this.data.edges);
  }

  protected abstract createGraphNodes(
    nodes: readonly SwimlaneDiagramNode[],
    tableGroupNode: INode
  ): void;

  protected createGraphEdges(edges: readonly SwimlaneDiagramEdge[]): void {
    this.data.edges.forEach((edge) => {
      // Note that nodes and groups need to have disjoint sets of ids, otherwise it is impossible to determine
      // which node is the correct source/target.
      const fromGraphNode = this.graphNodesById[edge.fromNodeId];
      if (!fromGraphNode) {
        throw new RangeError("missing from node: " + edge.fromNodeId);
      }
      const toGraphNode = this.graphNodesById[edge.toNodeId];
      if (!toGraphNode) {
        throw new RangeError("missing to node: " + edge.toNodeId);
      }

      this.graph.createEdge({
        source: fromGraphNode,
        target: toGraphNode,
        labels: [edge.label || ""],
        tag: edge,
      });
    });
  }

  protected createGraphNode(node: SwimlaneDiagramNode): INode {
    const size = node.size || [50, 50];
    const graphNode = this.graph.createNode({
      labels: [node.label || node.id],
      layout: new Rect(0, 0, size[0], size[1]),
      tag: node,
    });
    if (node.fill) {
      // If the node data specifies an individual fill color, adjust the style.
      this.graph.setStyle(
        graphNode,
        new ShapeNodeStyle({
          fill: node.fill,
          stroke: "white",
        })
      );
    }
    this.graphNodesById[node.id] = graphNode;
    return graphNode;
  }

  protected abstract createLanes(lanes: readonly SwimlaneDiagramLane[]): void;

  private createTableGroupNode(): INode {
    // Create a top-level group node and bind, via the TableNodeStyle, the table to it.
    const tableStyle = new TableNodeStyle(this.table);
    return this.graph.createGroupNode(
      null,
      this.table.layout.toRect(),
      tableStyle
    );
  }
}
