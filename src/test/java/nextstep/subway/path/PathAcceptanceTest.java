package nextstep.subway.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.acceptance.LineSectionAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private StationResponse 서울역;

    @BeforeEach
    void beforeEach() {
        super.setUp();

        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);
        서울역 = StationAcceptanceTest.지하철역_등록되어_있음("서울역").as(StationResponse.class);

        LineRequest lineRequest1 = new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10);
        LineRequest lineRequest2 = new LineRequest("이호선", "bg-red-600", 강남역.getId(), 교대역.getId(), 15);
        LineRequest lineRequest3 = new LineRequest("삼호선", "bg-red-600", 교대역.getId(), 양재역.getId(), 15);

        신분당선 = LineAcceptanceTest.지하철_노선_등록되어_있음(lineRequest1).as(LineResponse.class);
        이호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(lineRequest2).as(LineResponse.class);
        삼호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(lineRequest3).as(LineResponse.class);

        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(신분당선, 양재역, 서울역, 30);
        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 3);
        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(삼호선, 남부터미널역, 서울역, 10);
    }

    /*
    *   Given 2개 이상의 지하철역이 등록되어 있음
    *   And 1개 이상의 지하철 노선이 등록되어 있음
    *   And 지하철 노선에 지하철역이 등록되어 있음
    *   When 출발역과 도착역을 선택하고 경로를 검색하면
    *   Then 출발역에서 도착역까지의 최단거리 경로가 조회된다.
    */
    @DisplayName("같은 지하철 노선에 포함된 역 사이의 최단거리 경로를 조회한다.")
    @Test
    void findPath_sameLine() {
        // when
        ExtractableResponse<Response> response = 출발역_도착역_최단거리_경로_조회(강남역.getId(), 서울역.getId());

        // then
        출발역_도착역_최단거리_거리_경로_확인(response, 12, 강남역, 양재역, 서울역);
    }

    /*
     *   Given 2개 이상의 지하철역이 등록되어 있음
     *   And 1개 이상의 지하철 노선이 등록되어 있음
     *   And 지하철 노선에 지하철역이 등록되어 있음
     *   When 출발역과 도착역을 선택하고 경로를 검색하면
     *   Then 출발역에서 도착역까지의 최단거리 경로가 조회된다.
     */
    @DisplayName("서로 다른 지하철 노선에 포함된 역 사이의 최단거리 경로를 조회한다.")
    @Test
    void findPath_differentLines() {
        // when
        ExtractableResponse<Response> response = 출발역_도착역_최단거리_경로_조회(강남역.getId(), 남부터미널역.getId());

        // then
        출발역_도착역_최단거리_거리_경로_확인(response, 18, 강남역, 교대역, 남부터미널역);
    }

    @DisplayName("출발역과 도착역이 같은 경우 최단거리 경로를 조회할 수 없다.")
    @Test
    void findPath_throwsException_ifStationsAreSame() {
        // when
        ExtractableResponse<Response> response = 출발역_도착역_최단거리_경로_조회(강남역.getId(), 강남역.getId());

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("출발역과 도착역이 연결이 되어 있지 않으면 최단거리 경로를 조회할 수 없다.")
    @Test
    void findPath_throwsException_ifStationsAreNotConnected() {
        // given
        LineSectionAcceptanceTest.지하철_노선에_지하철역_제외_요청(신분당선, 서울역);
        LineSectionAcceptanceTest.지하철_노선에_지하철역_제외_요청(삼호선, 교대역);
        LineSectionAcceptanceTest.지하철_노선에_지하철역_제외_요청(삼호선, 양재역);

        // when
        ExtractableResponse<Response> response = 출발역_도착역_최단거리_경로_조회(강남역.getId(), 남부터미널역.getId());

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("존재하지 않은 출발역이나 도착역으로는 최단거리 경로를 조회할 수 없다.")
    @Test
    void findPath_throwsException_ifFromOrToNonExistentStation() {
        // given
        LineSectionAcceptanceTest.지하철_노선에_지하철역_제외_요청(삼호선, 남부터미널역);

        // when
        ExtractableResponse<Response> response = 출발역_도착역_최단거리_경로_조회(강남역.getId(), 남부터미널역.getId());

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static ExtractableResponse<Response> 출발역_도착역_최단거리_경로_조회(Long source, Long target) {
        Map<String, Long> param = new HashMap<>();
        param.put("source", source);
        param.put("target", target);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().queryParams(param).get("/paths")
                .then().log().all()
                .extract();
    }

    void 출발역_도착역_최단거리_거리_경로_확인(ExtractableResponse<Response> response, double distance, StationResponse... stations) {
        PathResponse path = response.as(PathResponse.class);
        assertThat(path.getStationResponses()).containsExactly(stations);
        assertThat(path.getDistance()).isEqualTo(distance);
    }
}
