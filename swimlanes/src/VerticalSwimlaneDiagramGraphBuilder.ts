import SwimlaneDiagramGraphBuilder from "./SwimlaneDiagramGraphBuilder";
import {IGraph, INode, Insets, ITable, VoidStripeStyle} from "yfiles";
import SwimlaneDiagramLane from "./SwimlaneDiagramLane";
import SwimlaneDiagramNode from "./SwimlaneDiagramNode";
import SwimlaneDiagramData from "./SwimlaneDiagramData";

export default class VerticalSwimlaneDiagramGraphBuilder extends SwimlaneDiagramGraphBuilder {
  constructor(data: SwimlaneDiagramData, graph: IGraph) {
    super(data, graph);

    const table = this.table;

    // Configure the row style, i.e. the container for the swimlanes. In this case, they should not be rendered at all,
    // since we are creating a vertical swimlane. However, in the general case of Tables, we could use a
    // semi-transparent style here, too create overlapping cell colors.
    table.rowDefaults.insets = new Insets(0, 10, 0, 0);
    table.rowDefaults.style = new VoidStripeStyle();

    // Configure the column style, i.e. the actual swimlanes.
    table.columnDefaults.insets = new Insets(10, 30, 10, 10);
    table.columnDefaults.labels.style = this.labelStyle;
    table.columnDefaults.size = 200;
    table.columnDefaults.style = this.laneStripeStyle;
    // Create the single container row.
    table.createRow();
  }

  protected createGraphNodes(
    nodes: readonly SwimlaneDiagramNode[],
    tableGroupNode: INode
  ) {
    nodes.forEach((node) => {
      const graphNode = this.createGraphNode(node);

      if (node.laneId) {
        // Nodes are assigned to lanes based on their center location. We could either place them manually by getting
        // the respective lane's bounds, or we can use a helper function to place nodes in specific cells. In case of
        // manually, placing the nodes, don't forget to also reparent the nodes to the table group, which is also done
        // by the helper function.
        ITable.placeNodeInCell(
          this.graph,
          graphNode,
          tableGroupNode,
          this.table.columns.elementAt(this.laneIndicesById[node.laneId]),
          this.table.rows.elementAt(0)
        );
      }
    });
  }

  protected createLanes(lanes: readonly SwimlaneDiagramLane[]) {
    const table = this.table;

    lanes.forEach((lane, index: number) => {
      const column = table.createColumn({
        tag: lane,
        style:
          index % 2 === 0
            ? this.laneStripeStyle
            : this.alternativeLaneStripeStyle,
      });
      table.addLabel(column, lane.label);
    });
  }
}
