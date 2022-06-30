package nextstep.subway.path.domain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.ShortestPathResponse;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;

public class PathFinder {
    private static final String SOURCE_AND_TARGET_IS_EQUAL_ERROR = "출발지와 도착지는 같을 수 없습니다.";
    private static final String SOURCE_OR_TARGET_IS_NOT_CONTAINS_ALL_LINE_STATION_ERROR = "대상 노선에서 해당역을 찾을 수 없습니다.";
    private static final String SOURCE_AND_TARGET_IS_NOT_CONNECTED_ERROR = "출발지와 도착지가 연결 되어있는지 확인하세요.";
    static WeightedMultigraph<Station, SectionWeightedEdge> graph;
    static {
        graph = new WeightedMultigraph<>(SectionWeightedEdge.class);
    }

    public static ShortestPathResponse findShortestPath(List<Line> allLines, Station source, Station target) {
        validate(allLines, source, target);
        addSectionsToGraph(allLines);

        DijkstraShortestPath<Station, SectionWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
        GraphPath<Station, SectionWeightedEdge> path = dijkstraShortestPath.getPath(source, target);
        checkResultIsNull(path);
        return ShortestPathResponse.of(path, toStationResponse(path.getVertexList()));
    }

    private static List<StationResponse> toStationResponse(List<Station> vertexStationList) {
        return vertexStationList.stream()
            .map(StationResponse::from)
            .collect(Collectors.toList());
    }

    private static void validate(List<Line> allLines, Station source, Station target) {
        validateStationEquals(source, target);
        validateStationContains(allLines, source, target);
    }

    private static void validateStationEquals(Station source, Station target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException(SOURCE_AND_TARGET_IS_EQUAL_ERROR);
        }
    }

    private static void validateStationContains(List<Line> allLines, Station source, Station target) {
        List<Station> stations = mergeAllLinesStations(allLines);
        if (!stations.contains(source) || !stations.contains(target)) {
            throw new IllegalArgumentException(SOURCE_OR_TARGET_IS_NOT_CONTAINS_ALL_LINE_STATION_ERROR);
        }
    }

    private static void addSectionsToGraph(List<Line> allLines) {
        addVertexByStation(mergeAllLinesStations(allLines));
        setEdgeWeightBySection(mergeAllLinesSections(allLines));
    }

    private static void addVertexByStation(List<Station> allLinesStations) {
        allLinesStations.forEach(it -> graph.addVertex(it));
    }

    private static void setEdgeWeightBySection(List<Section> allLinesSections) {
        allLinesSections.forEach(it -> {
            SectionWeightedEdge sectionWeightedEdge = new SectionWeightedEdge(it);
            graph.addEdge(it.getUpStation(), it.getDownStation(), sectionWeightedEdge);
            graph.setEdgeWeight(sectionWeightedEdge, it.getDistance());
        });
    }

    private static List<Station> mergeAllLinesStations(List<Line> allLines) {
        return allLines.stream()
            .map(Line::getAllStations)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private static List<Section> mergeAllLinesSections(List<Line> allLines) {
        return allLines.stream()
            .map(Line::getSections)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private static void checkResultIsNull(GraphPath<Station, SectionWeightedEdge> path) {
        if (path == null) {
            throw new IllegalArgumentException(SOURCE_AND_TARGET_IS_NOT_CONNECTED_ERROR);
        }
    }
}
