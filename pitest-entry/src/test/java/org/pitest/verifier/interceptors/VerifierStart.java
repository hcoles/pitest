package org.pitest.verifier.interceptors;

import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;

public class VerifierStart {

    public static InterceptorVerifier forInterceptor(MutationInterceptor interceptor) {
      return new InterceptorVerifier(interceptor);
    }

    public static InterceptorVerifier forInterceptorFactory(MutationInterceptorFactory f) {
        return new InterceptorVerifier(f.createInterceptor(null));
    }

}


