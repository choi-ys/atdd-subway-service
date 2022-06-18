package nextstep.subway.favorite.application;

import nextstep.subway.favorite.domain.Favorite;
import nextstep.subway.favorite.domain.FavoriteRepository;
import nextstep.subway.favorite.dto.FavoriteRequest;
import nextstep.subway.favorite.dto.FavoriteResponse;
import nextstep.subway.line.consts.ErrorMessage;
import nextstep.subway.member.application.MemberService;
import nextstep.subway.member.domain.Member;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final StationService stationService;
    private final MemberService memberService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           StationService stationService,
                           MemberService memberService) {
        this.favoriteRepository = favoriteRepository;
        this.stationService = stationService;
        this.memberService = memberService;
    }

    @Transactional
    public FavoriteResponse saveFavorite(Long memberId, FavoriteRequest request) {
        Member member = memberService.findMemberById(memberId);
        Station sourceStation = stationService.findStationById(request.getSource());
        Station targetStation = stationService.findStationById(request.getTarget());
        Favorite favorite = favoriteRepository.save(Favorite.of(member, sourceStation, targetStation));
        return FavoriteResponse.from(favorite);
    }

    public List<FavoriteResponse> findFavorites(Long memberId) {
        List<Favorite> favorites = favoriteRepository.findAllByMemberId(memberId);
        return favorites.stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFavorite(Long id, Long memberId) {
        Favorite favorite = favoriteRepository.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                String.format(ErrorMessage.ERROR_FAVORITE_NOT_FOUND, memberId, id))
        );
        favoriteRepository.delete(favorite);
    }

    public void deleteFavoriteByMemberId(Long memberId) {
        List<Favorite> favorites = favoriteRepository.findAllByMemberId(memberId);
        favoriteRepository.deleteAll(favorites);
    }
}
