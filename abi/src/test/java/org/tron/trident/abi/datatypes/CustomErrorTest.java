package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
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

    Iterator<TypeReference<?>> expectedParameter = parameters.iterator();
    for (TypeReference<?> actualParameter : event.getParameters()) {
      assertEquals(expectedParameter.next(), actualParameter);
    }
  }

}
