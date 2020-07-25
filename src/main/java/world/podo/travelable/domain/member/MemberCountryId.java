package world.podo.travelable.domain.member;

import lombok.*;
import org.springframework.util.Assert;
import world.podo.travelable.domain.country.Country;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberCountryId implements Serializable {
    private static final long serialVersionUID = 1729839338332735726L;

    private Long memberId;
    private Long countryId;

    public static MemberCountryId of(Member member, Country country) {
        Assert.notNull(member, "'member' must not be null");
        Assert.notNull(country, "'country' must not be null");
        return new MemberCountryId(
                member.getMemberId(),
                country.getCountryId()
        );
    }
}
