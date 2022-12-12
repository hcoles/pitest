package org.pitest.mutationtest.tdghistory;
import java.util.logging.Logger;
import org.pitest.mutationtest.incremental.WriterFactory;
import java.io.BufferedReader;
import java.util.Optional;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.io.IOException;
import org.pitest.util.Unchecked;
import java.io.ByteArrayInputStream;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.util.CheckSumUtil;
import org.pitest.classinfo.ClassName;
import java.io.PrintWriter;
import java.net.URL;
public class TdgHistoryStoreImpl implements TdgHistoryStore{
    // private static final Logger                                   LOG               = Log
    //   .getLogger();
    private final WriterFactory                                   outputFactory;
    private final BufferedReader                                  input;
    ProjectClassPaths classPath;
    CheckSumUtil checkSumUtil;
    private Map<URL, String>                                       previousHash = new HashMap<>();
    private final Map<MutationIdentifier, MutationStatusTestPair> previousResults   = new HashMap<>();
    public TdgHistoryStoreImpl(final WriterFactory output, final Optional<Reader> input, final ProjectClassPaths classPath) {
        this.outputFactory = output;
        this.input = createReader(input);
        this.classPath = classPath;
        checkSumUtil = new CheckSumUtil();
    }

    private BufferedReader createReader(Optional<Reader> input) {
        // System.out.println("createReader" + this.input);
        return input.map(BufferedReader::new)
                .orElse(null);
    }

    public void recordCheckSum() {
        final PrintWriter output = this.outputFactory.create();
        Collection<ClassName> codeAndtests = this.classPath.test();
        output.println(codeAndtests.size());
        for (ClassName name : codeAndtests) {
            URL url = this.classPath.getClassPath().findResource(name.asInternalName() + ".class");
            // System.out.println(url);
            String hashcode = checkSumUtil.getCheckSum(url);
            output.println(serialize(new Tdgsha(url, hashcode)));
        }
        output.flush();
    }

    private void restoreResults() {
        String line;
        try {
          line = this.input.readLine();
          while (line != null) {
            final IdResult result = deserialize(line, IdResult.class);
            this.previousResults.put(result.id, result.status);
            line = this.input.readLine();
          }
        } catch (final IOException e) {
        //   LOG.warning("Could not read previous results");
        }
      }

    @Override
    public void init() {
        if (this.input != null) {
            this.restoreHash();
            this.restoreResults();
            try {
                this.input.close();
              } catch (final IOException e) {
                throw Unchecked.translateCheckedException(e);
            }
        }
        this.recordClassHash();
    }
    private void restoreHash() {
        try {
            final long hashSize = Long.parseLong(this.input.readLine());
            for (int i = 0; i != hashSize; i++) {
              final Tdgsha sha = deserialize(this.input.readLine(),
              Tdgsha.class);
              this.previousHash.put(sha.getFileUrl(),sha.getCRC32Hash());
            }
          } catch (final IOException e) {
            // LOG.warning("Could not read previous classpath");
            System.out.println("Could not read previous classpath");
          }
    }
    @Override
    public void recordClassHash() {
        this.recordCheckSum();
    }
    @Override
    public void recordResult(MutationResult result) {
        final PrintWriter output = this.outputFactory.create();
        output.println(serialize(new TdgHistoryStoreImpl.IdResult(
        result.getDetails().getId(), result.getStatusTestPair())));
        output.flush();
    }
    @Override
    public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
        return this.previousResults;
    }

    @Override
    public Map<URL, String> getHistorySha() {
        return this.previousHash;

    }

    private <T> T deserialize(String string, Class<T> clazz) throws IOException {
        try {
          final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
              Base64.getDecoder().decode(string));
          final ObjectInputStream objectInputStream = new ObjectInputStream(
              byteArrayInputStream);
          return clazz.cast(objectInputStream.readObject());
        } catch (final ClassNotFoundException e) {
          throw Unchecked.translateCheckedException(e);
        }
      }
    
      private <T> String serialize(T t) {
        try {
          final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          final ObjectOutputStream objectOutputStream = new ObjectOutputStream(
              byteArrayOutputStream);
          objectOutputStream.writeObject(t);
          return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        }
      }

      private static class IdResult implements Serializable {
        private static final long    serialVersionUID = 1L;
        final MutationIdentifier     id;
        final MutationStatusTestPair status;
    
        IdResult(final MutationIdentifier id, final MutationStatusTestPair status) {
          this.id = id;
          this.status = status;
        }
    
      }
}
