package sample;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MyServiceAsync {
  void getData(AsyncCallback<String> callback);
}
