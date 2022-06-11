export default interface SwimlaneLineageData {
  readonly links: readonly {
    readonly linkType: string;
    readonly source: string;
    readonly target: string;
  }[];

  readonly nodes: readonly {
    readonly id: string;
    readonly label: string;
    readonly resourceType: string;
  }[];
}
