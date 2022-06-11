import * as React from "react";
import {ErrorInfo} from "react";

type Props = React.PropsWithChildren<{title: string}>;

export default class SwimlaneErrorBoundary extends React.Component<
  Props,
  {error: Error | null; errorInfo: ErrorInfo | null}
> {
  constructor(props: Props) {
    super(props);
    this.state = {error: null, errorInfo: null};
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(error);
    this.setState({error, errorInfo: info});
  }

  render() {
    const {children, title} = this.props;

    if (this.state.error) {
      return (
        <div
          className="error-boundary swa-error"
          title={`${title} component failed to render.`}
        >
          <i className="fas fa-exclamation-triangle" />
        </div>
      );
    }

    return children;
  }
}
