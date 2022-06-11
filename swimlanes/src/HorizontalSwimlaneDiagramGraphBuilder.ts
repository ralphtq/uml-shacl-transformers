import SwimlaneDiagramGraphBuilder from "./SwimlaneDiagramGraphBuilder";
import {IGraph, INode, Insets, ITable, VoidStripeStyle} from "yfiles";
import SwimlaneDiagramLane from "./SwimlaneDiagramLane";
import SwimlaneDiagramNode from "./SwimlaneDiagramNode";
import SwimlaneDiagramData from "./SwimlaneDiagramData";

export default class HorizontalSwimlaneDiagramGraphBuilder extends SwimlaneDiagramGraphBuilder {
  constructor(data: SwimlaneDiagramData, graph: IGraph) {
    super(data, graph);

    const table = this.table;

    table.columnDefaults.insets = new Insets(0, 10, 0, 0);
    table.columnDefaults.style = new VoidStripeStyle();

    table.rowDefaults.insets = new Insets(30, 10, 10, 10);
    table.rowDefaults.labels.style = this.labelStyle;
    // table.rowDefaults.size = 500;
    table.rowDefaults.style = this.laneStripeStyle;

    // Create the single container column
    const column = table.createColumn({
      minWidth: 800,
    });
  }

  protected createGraphNodes(
    nodes: readonly SwimlaneDiagramNode[],
    tableGroupNode: INode
  ) {
    nodes.forEach((node) => {
      const graphNode = this.createGraphNode(node);

      if (node.laneId) {
        ITable.placeNodeInCell(
          this.graph,
          graphNode,
          tableGroupNode,
          this.table.columns.elementAt(0),
          this.table.rows.elementAt(this.laneIndicesById[node.laneId])
        );
      }
    });
  }

  protected createLanes(lanes: readonly SwimlaneDiagramLane[]) {
    const table = this.table;

    lanes.forEach((lane, index: number) => {
      const row = table.createRow({
        tag: lane,
        style:
          index % 2 === 0
            ? this.laneStripeStyle
            : this.alternativeLaneStripeStyle,
      });
      table.addLabel(row, lane.label);
    });
  }
}
