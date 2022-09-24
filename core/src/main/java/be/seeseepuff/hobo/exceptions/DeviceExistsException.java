package be.seeseepuff.hobo.exceptions;

import io.smallrye.graphql.api.ErrorCode;
import lombok.experimental.StandardException;

@ErrorCode("device-exists")
@StandardException
public class DeviceExistsException extends RuntimeException
{
}
