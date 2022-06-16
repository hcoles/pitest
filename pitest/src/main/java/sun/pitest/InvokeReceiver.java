package sun.pitest;

public interface InvokeReceiver {

  void registerClass(int id, String className);

  void registerProbes(int classId, String methodName, String methodDesc,
      int firstProbe, int lastProbe);

}
