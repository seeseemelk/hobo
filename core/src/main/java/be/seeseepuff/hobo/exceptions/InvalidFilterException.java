package be.seeseepuff.hobo.exceptions;

import io.smallrye.graphql.api.ErrorCode;
import lombok.experimental.StandardException;

@ErrorCode("invalid-filter")
@StandardException
public class InvalidFilterException extends RuntimeException
{
}
