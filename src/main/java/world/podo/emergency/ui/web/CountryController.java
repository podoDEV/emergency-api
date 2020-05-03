package world.podo.emergency.ui.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import world.podo.emergency.application.CountryApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryApplicationService countryApplicationService;

    /**
     * 나라 목록을 조회합니다
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CountrySimpleResponse>>> getCountries(
            @RequestHeader("Authorization") String authorization,
            @ApiIgnore @ModelAttribute("memberId") Long memberId,
            @RequestParam(required = false) Boolean pinned,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.data(
                        countryApplicationService.getCountries(memberId, pinned, pageable).getContent()
                )
        );
    }

    /**
     * 나라 1개를 조회합니다
     */
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponse<CountryDetailResponse>> getCountries(
            @RequestHeader("Authorization") String authorization,
            @ApiIgnore @ModelAttribute("memberId") Long memberId,
            @PathVariable Long countryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.data(
                        countryApplicationService.getCountry(memberId, countryId)
                )
        );
    }

    /**
     * 나라 1개를 구독합니다
     * 이미 구독되어있는 나라인 경우 성공으로 응답합니다
     */
    @PostMapping("/{countryId}/pin")
    public ResponseEntity<ApiResponse> pin(
            @PathVariable Long countryId
    ) {
        // TODO: pin
        return ResponseEntity.noContent().build();
    }

    /**
     * 나라 1개의 구독을 취소합니다
     * 이미 구독취소된 나라를 요청해도 성공으로 응답합니다
     */
    @PostMapping("/{countryId}/unpin")
    public ResponseEntity<ApiResponse> unpin(
            @PathVariable Long countryId
    ) {
        // TODO: unpin
        return ResponseEntity.noContent().build();
    }
}
