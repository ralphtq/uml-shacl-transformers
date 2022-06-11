/**
 * Swimlane diagram (https://en.wikipedia.org/wiki/Swim_lane) component adapted from the yFiles example "Building swimlanes from data".
 *
 * Code was added to support both horizontal and vertical orientation. (The yFiles example only supported the latter.)
 */
import {
  GraphComponent,
  HierarchicLayout,
  LayoutExecutor,
  LayoutOrientation,
  MinimumNodeSizeStage,
  SimplexNodePlacer,
} from "yfiles";
import * as React from "react";
import {useEffect, useRef} from "react";
import SwimlaneDiagramData from "./SwimlaneDiagramData";
import SwimlaneDiagramOrientation from "./SwimlaneDiagramOrientation";
import VerticalSwimlaneDiagramGraphBuilder from "./VerticalSwimlaneDiagramGraphBuilder";
import HorizontalSwimlaneDiagramGraphBuilder from "./HorizontalSwimlaneDiagramGraphBuilder";

function runLayout(graphComponent: GraphComponent): Promise<any> {
  const layout = new HierarchicLayout();
  layout.componentLayoutEnabled = false;
  layout.layoutOrientation = LayoutOrientation.TOP_TO_BOTTOM;
  layout.orthogonalRouting = true;
  layout.recursiveGroupLayering = (
    layout.nodePlacer as SimplexNodePlacer
  ).barycenterMode = true;

  // We use Layout executor convenience method that already sets up the whole layout pipeline correctly
  const layoutExecutor = new LayoutExecutor({
    graphComponent,
    layout: new MinimumNodeSizeStage(layout),
    // Table layout is enabled by default already...
    configureTableLayout: true,
    duration: "0s",
    animateViewport: true,
  });

  // Apply an initial layout
  return layoutExecutor.start();
}

const SwimlaneDiagram: React.FunctionComponent<{
  data: SwimlaneDiagramData;
  orientation: SwimlaneDiagramOrientation;
}> = ({data, orientation}) => {
  const graphComponentContainerDivRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const graphComponent = new GraphComponent(
      graphComponentContainerDivRef.current!
    );
    const graph = graphComponent.graph;
    switch (orientation) {
      case SwimlaneDiagramOrientation.HORIZONTAL:
        new HorizontalSwimlaneDiagramGraphBuilder(data, graph).build();
        break;
      case SwimlaneDiagramOrientation.VERTICAL:
        new VerticalSwimlaneDiagramGraphBuilder(data, graph).build();
        break;
      default:
        throw new EvalError();
    }
    graphComponent.devicePixelRatio = window.devicePixelRatio || 1;
    runLayout(graphComponent);
  }, []);

  return (
    <div
      ref={graphComponentContainerDivRef}
      style={{height: "100%", width: "100%"}}
    />
  );
};

export default SwimlaneDiagram;
