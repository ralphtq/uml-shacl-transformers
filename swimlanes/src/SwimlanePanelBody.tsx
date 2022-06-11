import * as BACT_1001 from "./BACT_1001.json";
import * as React from "react";
import SwimlaneDiagram from "./SwimlaneDiagram";
import SwimlaneDiagramData from "./SwimlaneDiagramData";
import SwimlaneDiagramOrientation from "./SwimlaneDiagramOrientation";
import SwimlaneErrorBoundary from "./SwimlaneErrorBoundary";
import transformSwimlaneLineageDataToDiagramData from "./transformSwimlaneLineageDataToDiagramData";

const HORIZONTAL_SAMPLE_DATA: SwimlaneDiagramData =
  transformSwimlaneLineageDataToDiagramData(BACT_1001);
console.info(HORIZONTAL_SAMPLE_DATA);

const VERTICAL_SAMPLE_DATA: SwimlaneDiagramData = {
  nodes: [
    // {
    //   id: "0",
    //   label: "Unassigned",
    //   size: [80, 50],
    //   fill: "#DCDCDC",
    // },
    {
      id: "1",
      label: "Source",
      laneId: "lane0",
    },
    {
      id: "2",
      laneId: "lane0",
    },
    {
      id: "3",
      laneId: "lane0",
    },
    {
      id: "4",
      laneId: "lane0",
    },
    {
      id: "5",
      laneId: "lane1",
      fill: "#DC143C",
    },
    {
      id: "6",
      laneId: "lane1",
      fill: "#4169E1",
    },
    {
      id: "7",
      laneId: "lane1",
      fill: "#DC143C",
    },
    {
      id: "8",
      laneId: "lane2",
    },
    {
      id: "9",
      laneId: "lane0",
      size: [100, 50],
    },
    {
      id: "10",
      laneId: "lane2",
    },
    {
      id: "11",
      laneId: "lane2",
    },
    {
      id: "12",
      laneId: "lane2",
    },
    {
      id: "13",
      laneId: "lane2",
    },
    {
      id: "14",
      laneId: "lane2",
    },
    {
      id: "15",
      label: "Target",
      laneId: "lane0",
    },
  ],
  edges: [
    {
      fromNodeId: "1",
      toNodeId: "2",
    },
    {
      fromNodeId: "1",
      toNodeId: "8",
    },
    {
      fromNodeId: "1",
      toNodeId: "3",
    },
    {
      fromNodeId: "2",
      toNodeId: "4",
    },
    {
      fromNodeId: "2",
      toNodeId: "9",
    },
    {
      fromNodeId: "3",
      toNodeId: "5",
    },
    {
      fromNodeId: "4",
      toNodeId: "6",
    },
    {
      fromNodeId: "5",
      toNodeId: "7",
    },
    {
      fromNodeId: "6",
      toNodeId: "10",
    },
    {
      fromNodeId: "8",
      toNodeId: "11",
    },
    {
      fromNodeId: "9",
      toNodeId: "11",
    },
    {
      fromNodeId: "9",
      toNodeId: "12",
    },
    {
      fromNodeId: "11",
      toNodeId: "13",
    },
    {
      fromNodeId: "12",
      toNodeId: "14",
    },
    {
      fromNodeId: "12",
      toNodeId: "15",
    },
    {
      fromNodeId: "13",
      toNodeId: "15",
    },
    // {
    //   fromNodeId: "13",
    //   toNodeId: "0",
    // },
  ],
  lanes: [
    {
      id: "lane0",
      label: "Lane 1",
    },
    {
      id: "lane1",
      label: "Lane 2",
    },
    {
      id: "lane2",
      label: "Lane 3",
    },
  ],
};

const ORIENTATION = SwimlaneDiagramOrientation.HORIZONTAL;

const SwimlanePanelBody: React.FunctionComponent = () => (
  <SwimlaneErrorBoundary title="Swimlane panel">
    <SwimlaneDiagram
      data={
        ORIENTATION === SwimlaneDiagramOrientation.HORIZONTAL
          ? HORIZONTAL_SAMPLE_DATA
          : VERTICAL_SAMPLE_DATA
      }
      orientation={ORIENTATION}
    />
  </SwimlaneErrorBoundary>
);

export default SwimlanePanelBody;
