package world.podo.travelable.infrastructure.public_api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import world.podo.travelable.domain.notice.NoticeFetchDetailValue;
import world.podo.travelable.domain.notice.NoticeFetchService;
import world.podo.travelable.domain.notice.NoticeFetchSimpleValue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PublicApiNoticeFetchService implements NoticeFetchService {
    private final RestTemplate publicApiRestTemplate;
    private final String publicApiHost;
    private final String publicApiNoticeListPath;
    private final String publicApiNoticeOnePath;

    public PublicApiNoticeFetchService(
            @Qualifier("publicApiRestTemplate") RestTemplate publicApiRestTemplate,
            @Value("${public.api.host}") String publicApiHost,
            @Value("${public.api.path.notice-list}") String publicApiNoticeListPath,
            @Value("${public.api.path.notice-one}") String publicApiNoticeOnePath
    ) {
        this.publicApiRestTemplate = publicApiRestTemplate;
        this.publicApiHost = publicApiHost;
        this.publicApiNoticeListPath = publicApiNoticeListPath;
        this.publicApiNoticeOnePath = publicApiNoticeOnePath;
    }

    @Override
    public List<NoticeFetchSimpleValue> fetchByCountryCode(String countryCode) {
        URI requestUrl = UriComponentsBuilder.fromHttpUrl(publicApiHost)
                                             .path(publicApiNoticeListPath)
                                             .queryParams(PublicApiUtils.createQueryParams())
                                             .queryParam("isoCode1", countryCode)
                                             .build(true)
                                             .toUri();
        ResponseEntity<Map> responseEntity = publicApiRestTemplate.exchange(
                new RequestEntity<>(HttpMethod.GET, requestUrl), Map.class
        );
        if (!responseEntity.getStatusCode()
                           .is2xxSuccessful() || responseEntity.getBody() == null) {
            log.error("Failed to get notices. statusCode:" + responseEntity.getStatusCode());
            throw new CountryApiFailedException("Failed to get notices. statusCode:" + responseEntity.getStatusCode());
        }
        return this.resolveSimpleValue(responseEntity.getBody());
    }

    private List<NoticeFetchSimpleValue> resolveSimpleValue(Map resultMap) {
        try {
            Map<String, Object> responseMap = (Map<String, Object>) resultMap.get("response");
            Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
            Integer totalCount = (int) bodyMap.get("totalCount");
            if (totalCount == 0) {
                return Collections.emptyList();
            }
            Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
            Object item = itemsMap.get("item");
            if (item instanceof List) {
                return ((List<Map<String, Object>>) item).stream()
                                                         .map(this::toNoticeFetchSimpleValue)
                                                         .collect(Collectors.toList());
            } else if (item instanceof Map) {
                return Collections.singletonList(
                        this.toNoticeFetchSimpleValue((Map<String, Object>) item)
                );
            } else {
                return Collections.emptyList();
            }
        } catch (ClassCastException ex) {
            log.error("Failed to parse result. result:{}", resultMap, ex);
            return Collections.emptyList();
        }
    }

    private NoticeFetchSimpleValue toNoticeFetchSimpleValue(Map<String, Object> noticeFetchMap) {
        if (noticeFetchMap == null) {
            return null;
        }
        return new NoticeFetchSimpleValue(
                PublicApiUtils.get(noticeFetchMap, NoticeFetchSimpleValue.FieldName.ID),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchSimpleValue.FieldName.FILE_URL),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchSimpleValue.FieldName.TITLE),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchSimpleValue.FieldName.WRITTEN_DATE)
        );
    }

    @Override
    public NoticeFetchDetailValue fetchOne(String id) {
        URI requestUrl = UriComponentsBuilder.fromHttpUrl(publicApiHost)
                                             .path(publicApiNoticeOnePath)
                                             .queryParam("id", id)
                                             .queryParam("_type", "json")
                                             .build(true)
                                             .toUri();
        ResponseEntity<Map> responseEntity = publicApiRestTemplate.exchange(
                new RequestEntity<>(HttpMethod.GET, requestUrl), Map.class
        );
        if (!responseEntity.getStatusCode()
                           .is2xxSuccessful() || responseEntity.getBody() == null) {
            log.error("Failed to get notice. statusCode:" + responseEntity.getStatusCode());
            throw new CountryApiFailedException("Failed to get notice. statusCode:" + responseEntity.getStatusCode());
        }
        return this.resolveDetailValue(responseEntity.getBody());
    }

    private NoticeFetchDetailValue resolveDetailValue(Map resultMap) {
        try {
            Map<String, Object> responseMap = (Map<String, Object>) resultMap.get("response");
            Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
            Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
            Map<String, Object> item = (Map<String, Object>) itemsMap.get("item");
            return Optional.ofNullable(item)
                           .map(this::toNoticeFetchDetailValue)
                           .orElse(null);

        } catch (ClassCastException ex) {
            log.error("Failed to parse result. result:{}", resultMap, ex);
            return null;
        }
    }

    private NoticeFetchDetailValue toNoticeFetchDetailValue(Map<String, Object> noticeFetchMap) {
        if (noticeFetchMap == null) {
            return null;
        }
        return new NoticeFetchDetailValue(
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.ID),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.TITLE),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.TEXT_CONTENT),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.HTML_CONTENT),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.FILE_URL),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.COUNT),
                PublicApiUtils.get(noticeFetchMap, NoticeFetchDetailValue.FieldName.WRITTEN_DATE)
        );
    }
}
