package me.donghun.eventrestapiserver.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.donghun.eventrestapiserver.common.RestDocsConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

    @Test
    @DisplayName("이벤트 생성")
    void createEvents() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 9, 9, 20, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 9, 10, 20, 30))
                .beginEventDateTime(LocalDateTime.of(2021, 9, 11, 20, 30))
                .endEventDateTime(LocalDateTime.of(2021, 9, 12, 20, 30))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("id").value(Matchers.not(123456789)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(Matchers.not(EventStatus.PUBLISHED.name())))
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andDo(document("create-events",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query").description("link to query event"),
                                linkWithRel("update").description("link to update event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("name of event"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of enrollment event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of enrollment event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment of event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("id of event"),
                                fieldWithPath("name").description("name of event"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of enrollment event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of enrollment event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment of event"),
                                fieldWithPath("free").description("is free event?"),
                                fieldWithPath("offline").description("is offline event?"),
                                fieldWithPath("eventStatus").description("status of event"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query.href").description("link to query event"),
                                fieldWithPath("_links.update.href").description("link to update event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 생성 - 빈 입력")
    void createEventsEmptyInputs() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("_links.index").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 생성 - 잘못된 입력(비즈니스 로직에 맞지 않는 경우)")
    void createEventsWrongInputs() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 9, 12, 20, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 9, 11, 20, 30))
                .beginEventDateTime(LocalDateTime.of(2021, 9, 10, 20, 30))
                .endEventDateTime(LocalDateTime.of(2021, 9, 9, 20, 30))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[*].field").value(containsInAnyOrder("basePrice", "maxPrice", "endEventDateTime")))
                .andExpect(jsonPath("_links.index").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 생성 - 잘못된 입력(값 자체가 잘못된 경우)")
    void createEventsWrongInputs2() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("")
                .description("REST API")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 9, 12, 20, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 9, 11, 20, 30))
                .beginEventDateTime(LocalDateTime.of(2021, 9, 10, 20, 30))
                .endEventDateTime(LocalDateTime.of(2021, 9, 9, 20, 30))
                .basePrice(-1)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[*].field").value(containsInAnyOrder("name", "basePrice")))
                .andExpect(jsonPath("_links.index").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 목록 조회 - 전체 30개 이벤트 / 페이지당 10개 / 두 번째 페이지 조회")
    void getEventsList() throws Exception {
        IntStream.range(0, 30).forEach(this::generateEvents);

        mockMvc.perform(get("/api/events")
                        .param("page", "1") // 두 번째 페이지
                        .param("size", "10")
                        .param("sort", "name,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventModelList[0]._links.self").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("events-list",
                        links(
                                linkWithRel("first").description("link to first page"),
                                linkWithRel("prev").description("link to previous page"),
                                linkWithRel("self").description("link to self page"),
                                linkWithRel("next").description("link to next page"),
                                linkWithRel("last").description("link to last page"),
                                linkWithRel("profile").description("link to profile")
                        )
                ))
                .andDo(print());
    }

    private Event generateEvents(int i) {
        Event event = Event.builder()
                .name("event" + i)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 9, 9, 20, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 9, 10, 20, 30))
                .beginEventDateTime(LocalDateTime.of(2021, 9, 11, 20, 30))
                .endEventDateTime(LocalDateTime.of(2021, 9, 12, 20, 30))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        return this.eventRepository.save(event);
    }

    @Test
    @DisplayName("이벤트 조회")
    void getEvents() throws Exception {
        Event event = generateEvents(987654321);

        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-events")) // TODO 문서화
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 조회 - 없는 이벤트")
    void getEventsNotFound() throws Exception {
        mockMvc.perform(get("/api/events/987654321"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 수정")
    void updateEvents() throws Exception {
        Event event = generateEvents(987654321);

        EventDto eventDto = objectMapper.convertValue(event, EventDto.class);
        eventDto.setName("new name");
        eventDto.setDescription("new description");

        mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(jsonPath("name").value(eventDto.getName()))
                .andExpect(jsonPath("description").value(eventDto.getDescription()))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-events")) // TODO 문서화
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 수정 - 없는 이벤트")
    void updateEventsNotFound() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("new name")
                .description("new description")
                .build();

        mockMvc.perform(put("/api/events/98765432")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 수정 - 잘못된 입력(값 자체가 잘못된 경우)")
    void updateEventsWrongInputs() throws Exception {
        Event event = generateEvents(987654321);

        EventDto eventDto = objectMapper.convertValue(event, EventDto.class);
        eventDto.setName("new name");
        eventDto.setDescription("");
        eventDto.setBasePrice(-1);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("_links.index").exists())
                .andExpect(jsonPath("errors[*].field").value(containsInAnyOrder("basePrice", "description")))
                .andDo(print());
    }

    @Test
    @DisplayName("이벤트 수정 - 잘못된 입력(비즈니스 로직에 맞지 않는 경우)")
    void updateEventsWrongInputs2() throws Exception {
        Event event = generateEvents(987654321);

        EventDto eventDto = objectMapper.convertValue(event, EventDto.class);
        eventDto.setBasePrice(2);
        eventDto.setMaxPrice(1);
        eventDto.setEndEventDateTime(event.getBeginEventDateTime().minus(1L, ChronoUnit.DAYS));

        mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("_links.index").exists())
                .andExpect(jsonPath("errors[*].field").value(containsInAnyOrder("basePrice", "maxPrice", "endEventDateTime")))
                .andDo(print());
    }

}