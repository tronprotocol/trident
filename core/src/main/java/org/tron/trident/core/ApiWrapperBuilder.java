package org.tron.trident.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.tron.trident.core.interceptor.TimeoutInterceptor;
import org.tron.trident.utils.Numeric;
import org.tron.trident.utils.Strings;

public class ApiWrapperBuilder {
  @Getter
  private final String grpcEndpoint;
  @Getter
  private String grpcEndpointSolidity;
  @Getter
  private String hexPrivateKey;
  @Getter
  private boolean useTLS;
  @Getter
  private File trustCert; // Certificate for custom full node
  @Getter
  private String apiKey;
  @Getter
  private long timeoutMs; // unset, no timeout interceptor is added
  private final List<ClientInterceptor> customInterceptors = new ArrayList<>();

  public ApiWrapperBuilder(String grpcEndpoint, String grpcEndpointSolidity,
      String hexPrivateKey) {
    Preconditions.checkArgument(!Strings.isEmpty(grpcEndpoint), "grpcEndpoint is null or empty");
    this.grpcEndpoint = grpcEndpoint;
    withGrpcEndpointSolidity(grpcEndpointSolidity);
    withPrivateKey(hexPrivateKey);
  }

  public ApiWrapperBuilder(String grpcEndpoint) {
    Preconditions.checkArgument(!Strings.isEmpty(grpcEndpoint), "grpcEndpoint is null or empty");
    this.grpcEndpoint = grpcEndpoint;
  }

  /**
   * Enable TLS with custom certificate
   * <p>
   * Recommend using TLS 1.2 or TLS 1.3 for better security.
   * For generating self-signed certificates, tools like OpenSSL can be used
   * (e.g., <a href="https://docs.openssl.org/master/man1/openssl-req/">OpenSSL Req</a>).
   * </p>
   *
   * @param certFile The certificate file
   */
  public ApiWrapperBuilder withTLS(File certFile) {
    Preconditions.checkNotNull(certFile, "certFile is null");
    Preconditions.checkArgument(certFile.exists(),
        "cert file does not exist: " + certFile.getAbsolutePath());
    this.useTLS = true;
    this.trustCert = certFile;
    return this;
  }

  /**
   * Enable TLS using the system trust certificates.
   * <p>
   * A certificate previously set by {@link #withTLS(File)} is cleared.
   * </p>
   */
  public ApiWrapperBuilder withTLS() {
    this.useTLS = true;
    this.trustCert = null;
    return this;
  }

  /**
   * Set API key for TronGrid
   */
  public ApiWrapperBuilder withApiKey(String apiKey) {
    Preconditions.checkArgument(!Strings.isEmpty(apiKey), "apiKey is empty");
    this.apiKey = apiKey;
    return this;
  }

  /**
   * Set timeout in milliseconds for all requests
   */
  public ApiWrapperBuilder withTimeout(long timeoutMs) {
    Preconditions.checkArgument(timeoutMs > 0, "timeout should be greater than 0");
    this.timeoutMs = timeoutMs;
    return this;
  }

  /**
   * Add multiple custom interceptors. Null elements are ignored.
   * <p>
   * Custom interceptors are applied outside the timeout interceptor, so they cannot
   * change the deadline set by {@link #withTimeout(long)}.
   * </p>
   */
  public ApiWrapperBuilder addInterceptors(List<ClientInterceptor> interceptors) {
    Preconditions.checkArgument(interceptors != null, "interceptors is null");
    for (ClientInterceptor interceptor : interceptors) {
      if (interceptor != null) {
        customInterceptors.add(interceptor);
      }
    }
    return this;
  }

  /**
   * set grpcEndpointSolidity
   */
  public ApiWrapperBuilder withGrpcEndpointSolidity(String grpcEndpointSolidity) {
    Preconditions.checkArgument(!Strings.isEmpty(grpcEndpointSolidity),
        "grpcEndpointSolidity is null or empty");
    this.grpcEndpointSolidity = grpcEndpointSolidity;
    return this;
  }

  /**
   * set PrivateKey, an optional "0x" prefix is accepted
   */
  public ApiWrapperBuilder withPrivateKey(String hexPrivateKey) {
    String cleaned = Numeric.cleanHexPrefix(hexPrivateKey);
    Preconditions.checkArgument(cleaned != null && cleaned.length() == 64,
        "hexPrivateKey should be 64 hex characters (32 bytes)");
    this.hexPrivateKey = cleaned;
    return this;
  }

  /**
   * The custom interceptors added so far, as an unmodifiable view.
   */
  public List<ClientInterceptor> getCustomInterceptors() {
    return Collections.unmodifiableList(customInterceptors);
  }

  /**
   * Assemble the final interceptor list: TimeoutInterceptor first (innermost, so the
   * configured deadline is applied last and cannot be overridden by other interceptors,
   * per gRPC's reverse-order execution), then the API-key header, then custom ones.
   */
  List<ClientInterceptor> buildInterceptors() {
    List<ClientInterceptor> interceptors = new ArrayList<>();
    if (timeoutMs > 0) {
      interceptors.add(new TimeoutInterceptor(timeoutMs));
    }
    if (!Strings.isEmpty(apiKey)) {
      Metadata header = new Metadata();
      Metadata.Key<String> key =
          Metadata.Key.of("TRON-PRO-API-KEY", Metadata.ASCII_STRING_MARSHALLER);
      header.put(key, apiKey);
      interceptors.add(MetadataUtils.newAttachHeadersInterceptor(header));
    }
    interceptors.addAll(customInterceptors);
    return interceptors;
  }

  public ApiWrapper build() {
    return new ApiWrapper(this);
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("grpcEndpoint", grpcEndpoint)
        .add("grpcEndpointSolidity", grpcEndpointSolidity)
        .add("hexPrivateKey", hexPrivateKey != null ? "****" : null)
        .add("useTLS", useTLS)
        .add("trustCert", trustCert != null ? trustCert.getAbsolutePath() : null)
        .add("apiKey", apiKey != null ? "****" : null)
        .add("timeoutMs", timeoutMs)
        .add("customInterceptors", customInterceptors)
        .toString();
  }

}
