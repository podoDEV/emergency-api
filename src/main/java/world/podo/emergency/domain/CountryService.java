package world.podo.emergency.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@DomainService
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;
    private final MemberCountryRepository memberCountryRepository;

    public Page<Country> getCountries(Long memberId, Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return countryRepository.findAll(pageable);
        }
        if (pinned) {
            return memberCountryRepository.findByMember_memberId(memberId, pageable).map(MemberCountry::getCountry);
        } else {
            // TODO pinned false
            return Page.empty(pageable);
        }
    }

    public Country getCountry(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(CountryNotFoundException::new);
    }
}
