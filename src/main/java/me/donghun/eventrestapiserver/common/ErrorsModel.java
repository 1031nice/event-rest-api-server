package me.donghun.eventrestapiserver.common;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import me.donghun.eventrestapiserver.index.IndexController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsModel extends RepresentationModel<ErrorsModel> {

    private final Errors errors;

    public ErrorsModel(Errors errors) {
        this.errors = errors;
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }

    public Errors getErrors() {
        return errors;
    }

}
