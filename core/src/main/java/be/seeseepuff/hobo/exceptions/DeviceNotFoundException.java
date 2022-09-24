package be.seeseepuff.hobo.exceptions;

import io.smallrye.graphql.api.ErrorCode;
import lombok.experimental.StandardException;

@ErrorCode("device-not-found")
@StandardException
public class DeviceNotFoundException extends RuntimeException
{
}
