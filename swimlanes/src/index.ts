import {License} from "yfiles";
// import "yfiles/yfiles.css";
import {PanelTypes} from "TB";
import SwimlanePanel from "./SwimlanePanel";

License.value = {
  comment: "7e2799de-2964-47fa-8cdd-22ad628035f3",
  date: "06/21/2021",
  distribution: false,
  domains: ["*"],
  expires: "08/21/2021",
  fileSystemAllowed: true,
  licensefileversion: "1.1",
  localhost: true,
  oobAllowed: true,
  package: "complete",
  product: "yFiles for HTML",
  type: "eval",
  version: "2.3",
  watermark:
    "yFiles HTML Evaluation License (expires in ${license-days-remaining} days)",
  key: "7b9ab3b7b5be78aeda568fdf9399d42cf4aa89ab",
};

PanelTypes.register(SwimlanePanel);
