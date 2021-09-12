package me.donghun.eventrestapiserver.events;

import me.donghun.eventrestapiserver.common.ErrorsModel;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Event event = modelMapper.map(eventDto, Event.class);
        // TODO EventService
        event.update();
        Event newEvent = eventRepository.save(event);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventModel eventModel = new EventModel(event);
        eventModel.add(linkTo(EventController.class).withRel("query"));
        eventModel.add(selfLinkBuilder.withRel("update"));
        eventModel.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventModel);
    }

    @GetMapping
    public ResponseEntity getEventsList(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = eventRepository.findAll(pageable);
        PagedModel<EventModel> eventModels = assembler.toModel(page, EventModel::new);
        eventModels.add(Link.of("/docs/index.html#resources-events-list").withRel("profile"));
        return ResponseEntity.ok(eventModels);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvents(@PathVariable Integer id) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        else {
            EventModel eventModel = new EventModel(optionalEvent.get());
            eventModel.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
            return ResponseEntity.ok(eventModel);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvents(@RequestBody @Valid EventDto eventDto,
                                       Errors errors,
                                       @PathVariable Integer id) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        else if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Event existingEvent = optionalEvent.get();
        modelMapper.map(eventDto, existingEvent);
        // TODO EventService
        existingEvent.update();
        // transaction이 아니기 때문에 dirty checking에 의해 write behind 되지 않아서 명시적으로 save 호출
        Event updatedEvent = eventRepository.save(existingEvent);
        EventModel eventModel = new EventModel(updatedEvent);
        eventModel.add(Link.of("/docs/index.html#resources-events-update").withRel("profile"));
        return ResponseEntity.ok(eventModel);
    }

}
