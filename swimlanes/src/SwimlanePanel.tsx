import * as React from "react";
import {AppContext, Panel, PanelType} from "TB";
import SwimlanePanelBody from "./SwimlanePanelBody";

export default class SwimlanePanel extends React.Component<any> {
  static contextType: any;
  static panelType: any;

  render() {
    const {panelId, ...panelProps} = this.props;

    return (
      <Panel
        {...panelProps}
        body={<SwimlanePanelBody />}
        className="SwimlanesPanel"
        panel={this}
        panelId={panelId}
        title="Swimlanes"
      />
    );
  }
}

SwimlanePanel.contextType = AppContext;
SwimlanePanel.panelType = new PanelType({
  name: "SwimlanesPanel",
  labels: {
    en: "Swimlanes",
  },
  group: PanelType.ExploreGroup,
  iconClass: "fas fa-grip-lines",
  order: 0,
  settings: [],
});
