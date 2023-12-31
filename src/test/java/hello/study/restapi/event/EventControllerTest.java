package hello.study.restapi.event;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hello.study.restapi.common.BaseControllerTest;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class EventControllerTest extends BaseControllerTest {

	@Autowired
	EventRepository eventRepository;

	@Test
	@DisplayName("정상적으로 이벤트를 생성하는 테스트")
	void createEvent() throws Exception {
		EventDto eventDto = EventDto.builder()
			.name("Kim's")
			.description("HBD Party")
			.beginEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 0, 0))
			.closeEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 23, 59))
			.beginEventDateTime(LocalDateTime.of(2023, 11, 17, 23, 59))
			.endEventDateTime(LocalDateTime.of(2023, 11, 18, 23, 59))
			.basePrice(10_000)
			.maxPrice(100_000)
			.location("Kim's House")
			.build();

		mockMvc.perform(post("/api/events")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaTypes.HAL_JSON_VALUE) // accept Header
				.content(objectMapper.writeValueAsString(eventDto)))
			.andDo(print()) // show Result
			.andExpect(status().isCreated()) // isCreated == 201
			.andExpect(jsonPath("free").value(false))
			.andExpect(jsonPath("offline").value(true))
			.andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
			.andExpect(jsonPath("_links.self").exists())
			.andExpect(jsonPath("_links.query-events").exists())
			.andExpect(jsonPath("_links.update-event").exists())
			.andDo(document("create-event",
				links(
					linkWithRel("self").description("link to self"),
					linkWithRel("query-events").description("link to query-events"),
					linkWithRel("update-event").description("link to update-event"),
					linkWithRel("profile").description("link to profile")
				),
				requestHeaders(
					headerWithName(HttpHeaders.ACCEPT).description("accept header"),
					headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
				),
				requestFields(
					fieldWithPath("name").description("Name of new event"),
					fieldWithPath("description").description("description of new event"),
					fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
					fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
					fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
					fieldWithPath("endEventDateTime").description("date time of end of new event"),
					fieldWithPath("location").description("location of new event"),
					fieldWithPath("basePrice").description("base price of new event"),
					fieldWithPath("maxPrice").description("max price of new event"),
					fieldWithPath("limitOfEnrollment").description("limit of enrollment")
				),
				responseHeaders(
					headerWithName(HttpHeaders.LOCATION).description("Location header"),
					headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
				),
				relaxedResponseFields(
					fieldWithPath("id").description("identifier of new event"),
					fieldWithPath("name").description("Name of new event"),
					fieldWithPath("description").description("description of new event"),
					fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
					fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
					fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
					fieldWithPath("endEventDateTime").description("date time of end of new event"),
					fieldWithPath("location").description("location of new event"),
					fieldWithPath("basePrice").description("base price of new event"),
					fieldWithPath("maxPrice").description("max price of new event"),
					fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
					fieldWithPath("free").description("it tells if this event is free or not"),
					fieldWithPath("offline").description("it tells if this event is offline event or not"),
					fieldWithPath("eventStatus").description("event status"),
					fieldWithPath("_links.self.href").description("link to self"),
					fieldWithPath("_links.query-events.href").description("link to query events"),
					fieldWithPath("_links.update-event.href").description("link to update event"),
					fieldWithPath("_links.profile.href").description("link to profile")
				)
			))
		;
	}

	@Test
	@DisplayName("입력 받을 수 없는 값을 사용한 경우 에러가 발생하는 테스트")
	void createEvent_Bad_Request() throws Exception {
		Event event = Event.builder()
			.id(100)
			.name("Kim's")
			.description("HBD Party")
			.beginEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 0, 0))
			.closeEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 23, 59))
			.beginEventDateTime(LocalDateTime.of(2023, 11, 17, 23, 59))
			.endEventDateTime(LocalDateTime.of(2023, 11, 18, 23, 59))
			.basePrice(10_000)
			.maxPrice(100_000)
			.location("Kim's House")
			.free(true)
			.offline(false)
			.eventStatus(EventStatus.PUBLISHED)
			.build();

		mockMvc.perform(post("/api/events")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaTypes.HAL_JSON_VALUE) // accept Header
				.content(objectMapper.writeValueAsString(event)))
			.andDo(print()) // show Result
			.andExpect(status().isBadRequest())
		;
	}

	@Test
	@DisplayName("입력 값이 비어있으면 에러가 발생하는 테스트")
	void createEvent_Bad_Request_Empty_Input() throws Exception {
		EventDto eventDto = EventDto.builder().build();

		mockMvc.perform(post("/api/events")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(eventDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("입력 값이 잘못된 경우 에러가 발생하는 테스트")
	void createEvent_Bad_Request_Wrong_Input() throws Exception {
		EventDto eventDto = EventDto.builder()
			.name("Kim's")
			.description("HBD Party")
			.beginEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 0, 0))
			.closeEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 23, 59))
			.beginEventDateTime(LocalDateTime.of(2023, 11, 12, 23, 59))
			.endEventDateTime(LocalDateTime.of(2023, 11, 11, 23, 59))
			.basePrice(100_000)
			.maxPrice(10_000)
			.location("Kim's House")
			.build();

		mockMvc.perform(post("/api/events")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(eventDto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors[0].objectName").exists())
			.andExpect(jsonPath("errors[0].defaultMessage").exists())
			.andExpect(jsonPath("errors[0].code").exists())
			.andExpect(jsonPath("_links.index").exists())
		;
	}

	@Test
	@DisplayName("30개의 이벤트를 10개씩 조회하기 현재 두번째 페이지")
	void queryEvents() throws Exception {
		IntStream.range(0, 30).forEach(this::generateEvent);

		mockMvc.perform(get("/api/events")
				.param("page", "1")
				.param("size", "10")
				.param("sort", "name,DESC")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("page").exists())
			.andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
			.andExpect(jsonPath("_links.self").exists())
			.andExpect(jsonPath("_links.profile").exists())
			.andDo(document("query-events"))
		;
	}

	@Test
	@DisplayName("이벤트 하나 조회")
	void getEvent() throws Exception {
		Event event = generateEvent(99);

		mockMvc.perform(get("/api/events/{id}", event.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("name").exists())
			.andExpect(jsonPath("id").exists())
			.andExpect(jsonPath("_links.self").exists())
			.andExpect(jsonPath("_links.profile").exists())
			.andDo(document("get-an-event"))
		;
	}

	@Test
	@DisplayName("없는 이벤트 조회시 404")
	void getEvent_404() throws Exception {
		mockMvc.perform(get("/api/event/1116"))
			.andExpect(status().isNotFound());
	}


	@Test
	@DisplayName("이벤트 수정")
	void updateEvent() throws Exception {
		Event event = generateEvent(100);
		EventDto eventDto = modelMapper.map(event, EventDto.class);
		String updateName = "update name";
		eventDto.setName(updateName);

		mockMvc.perform(put("/api/events/{id}", event.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(eventDto)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("name").value(updateName))
			.andExpect(jsonPath("_links.self").exists());
	}

	@Test
	@DisplayName("이벤트 수정 실패 - 빈 값")
	void updateEvent_400_empty() throws Exception {
		Event event = generateEvent(100);
		EventDto eventDto = new EventDto();

		mockMvc.perform(put("/api/events/{id}", event.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(eventDto)))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("이벤트 수정 실패 - 잘못된 값")
	void updateEvent_400_wrong() throws Exception {
		Event event = generateEvent(100);

		EventDto eventDto = modelMapper.map(event, EventDto.class);
		eventDto.setBasePrice(40000);
		eventDto.setMaxPrice(1000);

		mockMvc.perform(put("/api/events/{id}", event.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(eventDto)))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("이벤트 수정 실패 - 존재하지 않는 이벤트")
	void updateEvent_404() throws Exception {
		Event event = generateEvent(66);
		EventDto eventDto = modelMapper.map(event, EventDto.class);

		mockMvc.perform(put("/api/events/4000000")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(eventDto)))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	private Event generateEvent(int index) {
		Event event = Event.builder()
			.name("event " + index)
			.description("test event")
			.beginEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 0, 0))
			.closeEnrollmentDateTime(LocalDateTime.of(2023, 11, 16, 23, 59))
			.beginEventDateTime(LocalDateTime.of(2023, 11, 17, 23, 59))
			.endEventDateTime(LocalDateTime.of(2023, 11, 18, 23, 59))
			.basePrice(10_000)
			.maxPrice(100_000)
			.location("Kim's House")
			.free(false)
			.offline(true)
			.eventStatus(EventStatus.DRAFT)
			.build();

		return eventRepository.save(event);
	}
}