package world.podo.travelable.domain.notice;

import lombok.RequiredArgsConstructor;
import world.podo.travelable.domain.DomainService;
import world.podo.travelable.domain.country.Country;

@DomainService
@RequiredArgsConstructor
public class NoticeSynchronizationService {
    private final NoticeFactory noticeFactory;
    private final NoticeRepository noticeRepository;

    public Notice synchronize(Country country, NoticeFetchDetailValue noticeFetchDetailValue) {
        if (noticeFetchDetailValue == null) {
            return null;
        }
        return noticeRepository.findByProviderNoticeId(noticeFetchDetailValue.getId())
                               .map(it -> {
                                   Notice notice = it.update(
                                           noticeFetchDetailValue.getId(),
                                           noticeFetchDetailValue.getTitle(),
                                           noticeFetchDetailValue.getTextContent(),
                                           noticeFetchDetailValue.getHtmlContent(),
                                           noticeFetchDetailValue.getWrittenDate(),
                                           country
                                   );
                                   return notice;
                               })
                               .orElseGet(() -> {
                                   Notice notice = noticeFactory.create(
                                           noticeFetchDetailValue.getId(),
                                           noticeFetchDetailValue.getTitle(),
                                           noticeFetchDetailValue.getTextContent(),
                                           noticeFetchDetailValue.getHtmlContent(),
                                           noticeFetchDetailValue.getWrittenDate(),
                                           country
                                   );
                                   return notice;
                               });
    }
}
