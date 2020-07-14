package world.podo.emergency.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.podo.emergency.domain.country.CovidFetchService;
import world.podo.emergency.domain.notice.NoticeFetchService;
import world.podo.emergency.ui.web.CountryResponse;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SampleApplicationService {
    private final CovidFetchService covidFetchService;
    private final NoticeFetchService noticeFetchService;

    public List<CountryResponse> getSampleResponses() {
        return Arrays.asList(
                CountryResponse.ghana(covidFetchService, noticeFetchService),
                CountryResponse.gabon(covidFetchService, noticeFetchService),
                CountryResponse.guyana(covidFetchService, noticeFetchService)
        );
    }
}
