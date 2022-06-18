package nextstep.subway.line.application;

import nextstep.subway.auth.domain.LoginMember;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.PathFinder;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PathService {
    private final StationService stationService;
    private final LineService lineService;
    private final PathFinder pathFinder;

    public PathService(StationService stationService, LineService lineService, PathFinder pathFinder) {
        this.stationService = stationService;
        this.lineService = lineService;
        this.pathFinder = pathFinder;
        this.pathFinder.addLines(this.lineService.findLines());
    }

    public PathResponse findShortestPath(int age, Long sourceStationId, Long targetStationId) {
        Station sourceStation = stationService.findStationById(sourceStationId);
        Station targetStation = stationService.findStationById(targetStationId);

        return PathResponse.from(pathFinder.findShortestPath(sourceStation, targetStation, age));
    }
}
