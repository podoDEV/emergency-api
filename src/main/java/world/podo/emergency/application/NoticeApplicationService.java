package world.podo.emergency.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.podo.emergency.domain.notice.NoticeService;
import world.podo.emergency.ui.web.NoticeResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeApplicationService {
    private final NoticeService noticeService;
    private final NoticeAssembler noticeAssembler;

    public Page<NoticeResponse> getNotices(Pageable pageable) {
        return noticeService.getNotices(pageable)
                            .map(noticeAssembler::toNoticeResponse);
    }
}
