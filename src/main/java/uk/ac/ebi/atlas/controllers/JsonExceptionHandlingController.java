package uk.ac.ebi.atlas.controllers;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

public abstract class JsonExceptionHandlingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonExceptionHandlingController.class);

    @ExceptionHandler(BioentityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleException(BioentityNotFoundException e, HttpServletResponse response) {
        writeJsonErrorMessage(e, response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public void handleException(Exception e, HttpServletResponse response) {
        writeJsonErrorMessage(e, response);
    }

    private static void writeJsonErrorMessage(Exception originatingException, HttpServletResponse response) {
        LOGGER.error(
                "{} - {}",
                originatingException.getMessage(),
                Joiner.on("\n\t").join(originatingException.getStackTrace()));
        response.setHeader("content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            response.getWriter().println(
                    GSON.toJson(
                            jsonError(isBlank(originatingException.getMessage()) ?
                                    "Unknown error" :
                                    originatingException.getMessage())));
        } catch (IOException _e) {
            throw new UncheckedIOException(_e);
        }
    }

    private static JsonObject jsonError(String message) {
        JsonObject result = new JsonObject();
        result.addProperty("error", message);
        return result;
    }
}
