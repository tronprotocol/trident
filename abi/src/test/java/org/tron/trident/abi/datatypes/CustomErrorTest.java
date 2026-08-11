package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.generated.Uint256;

public class CustomErrorTest {
  @Test
  public void testCreation() {

    List<TypeReference<?>> parameters =
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Address>() {}, new TypeReference<Uint256>() {});
    CustomError event = new CustomError("MyError", parameters);

    assertEquals(event.getName(), "MyError");

    List<TypeReference<Type>> actualParameters = event.getParameters();
    assertEquals(parameters.size(), actualParameters.size(),
        "parameter count mismatch");
    for (int i = 0; i < parameters.size(); i++) {
      assertEquals(parameters.get(i), actualParameters.get(i));
    }
  }

}
