package org.tron.trident.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.tron.trident.core.interceptor.TimeoutInterceptor;
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
  private List<ClientInterceptor> interceptors = new ArrayList<>();// Default: no timeout

  public ApiWrapperBuilder(String grpcEndpoint, String grpcEndpointSolidity,
      String hexPrivateKey) {
    Preconditions.checkArgument(grpcEndpoint != null, "grpcEndpoint is null");
    Preconditions.checkArgument(grpcEndpointSolidity != null, "grpcEndpointSolidity is null");
    Preconditions.checkArgument(hexPrivateKey != null && hexPrivateKey.length() == 64,
        "hexPrivateKey should be 64 hex characters (32 bytes)");

    this.grpcEndpoint = grpcEndpoint;
    this.grpcEndpointSolidity = grpcEndpointSolidity;
    this.hexPrivateKey = hexPrivateKey;
  }

  public ApiWrapperBuilder(String grpcEndpoint) {
    Preconditions.checkArgument(grpcEndpoint != null, "grpcEndpoint is null");
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
    Preconditions.checkArgument(certFile.exists(), "cert file does not exist: " + certFile.getAbsolutePath());
    this.useTLS = true;
    this.trustCert = certFile;
    return this;
  }

  /**
   * Enable TLS
   */
  public ApiWrapperBuilder withTLS() {
    this.useTLS = true;
    return this;
  }

  /**
   * Set API key for TronGrid
   */
  public ApiWrapperBuilder withApiKey(String apiKey) {
    Preconditions.checkArgument(!Strings.isEmpty(apiKey), "apiKey is empty");
    Metadata header = new Metadata();
    Metadata.Key<String> key =
        Metadata.Key.of("TRON-PRO-API-KEY", Metadata.ASCII_STRING_MARSHALLER);
    header.put(key, apiKey);
    interceptors.add(MetadataUtils.newAttachHeadersInterceptor(header));
    return this;
  }

  /**
   * Set timeout in milliseconds for all requests
   */
  public ApiWrapperBuilder withTimeout(long timeoutMs) {
    Preconditions.checkArgument(timeoutMs > 0, "timeout should be greater than 0");
    this.interceptors.add(new TimeoutInterceptor(timeoutMs));
    return this;
  }

  /**
   * Add multiple custom interceptors
   */
  public ApiWrapperBuilder withInterceptors(List<ClientInterceptor> interceptors) {
    Preconditions.checkArgument(interceptors != null, "interceptors is null");
    this.interceptors.addAll(interceptors);
    return this;
  }

  /**
   * set grpcEndpointSolidity
   */
  public ApiWrapperBuilder withGrpcEndpointSolidity(String grpcEndpointSolidity) {
    Preconditions.checkArgument(grpcEndpointSolidity != null, "grpcEndpointSolidity is null");
    this.grpcEndpointSolidity = grpcEndpointSolidity;
    return this;
  }

  /**
   * set PrivateKey
   */
  public ApiWrapperBuilder withPrivateKey(String hexPrivateKey) {
    Preconditions.checkArgument(hexPrivateKey != null && hexPrivateKey.length() == 64,
        "hexPrivateKey should be 64 hex characters (32 bytes)");
    this.hexPrivateKey = hexPrivateKey;
    return this;
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
        .add("interceptors", interceptors)
        .toString();
  }

}
