import SwimlaneDiagramData from "./SwimlaneDiagramData";
import SwimlaneLineageData from "./SwimlaneLineageData";

const transformSwimlaneLineageDataToDiagramData = (
  lineageData: SwimlaneLineageData
): SwimlaneDiagramData => ({
  lanes: Array.from(
    new Set(lineageData.nodes.map((node) => node.resourceType)).values()
  ).map((resourceType) => ({
    id: resourceType,
    label: resourceType,
  })),
  edges: lineageData.links.map((link) => ({
    fromNodeId: link.source,
    toNodeId: link.target,
  })),
  nodes: lineageData.nodes.map((node) => ({
    id: node.id,
    label: node.label,
    laneId: node.resourceType,
  })),
});

export default transformSwimlaneLineageDataToDiagramData;
