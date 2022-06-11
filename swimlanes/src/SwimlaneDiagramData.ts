import SwimlaneDiagramEdge from "./SwimlaneDiagramEdge";
import SwimlaneDiagramLane from "./SwimlaneDiagramLane";
import SwimlaneDiagramNode from "./SwimlaneDiagramNode";

export default interface SwimlaneDiagramData {
  readonly edges: readonly SwimlaneDiagramEdge[];
  readonly lanes: readonly SwimlaneDiagramLane[];
  readonly nodes: readonly SwimlaneDiagramNode[];
}
