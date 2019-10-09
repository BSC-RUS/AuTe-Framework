/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

package ru.bsc.test.at.mock.mq.components;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.bsc.test.at.mock.exception.NotFoundException;
import ru.bsc.test.at.mock.mq.models.GroupedMockMessages;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.models.MockMessagesGroup;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/mq-mock/__admin")
@Api(tags = {"MqMock"})
@RequiredArgsConstructor
public class ApiController {
    private final MqRunnerComponent mqRunnerComponent;

    @ApiOperation(value = "MQ mapping creation", notes = "Creates new mapping for MQ mock", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = String.class),
                    @ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
            }
    )
    @PostMapping("add-mapping")
    @ResponseBody
    public String addMapping(@RequestBody MockMessage mockMessage) {
        return mqRunnerComponent.addMapping(mockMessage);
    }

    @ApiOperation(value = "MQ mapping removing", notes = "Deletes mapping by mapping guid", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @DeleteMapping("mappings/{guid}")
    public void deleteMapping(@PathVariable String guid) throws IOException, TimeoutException {
        mqRunnerComponent.deleteMapping(guid);
    }

    @ApiOperation(value = "MQ mapping updating", notes = "Update mapping by guid", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @PostMapping("mappings/{guid}")
    public void updateMapping(@PathVariable String guid, @RequestBody MockMessage mapping) {
        mqRunnerComponent.updateMapping(guid, mapping);
    }

    @ApiOperation(value = "MQ mapping list getting", notes = "Gets list of all mocks", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @GetMapping("mappings")
    public List<MockMessage> getMappingList() {
        return mqRunnerComponent.getMappings().stream()
                .sorted(Comparator.comparing(valueOrDefault(MockMessage::getGroup))
                        .thenComparing(valueOrDefault(MockMessage::getName))
                        .thenComparing(valueOrDefault(MockMessage::getSourceQueueName)))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "MQ grouped mappings getting", notes = "Gets grouped JMS mocks", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @GetMapping("mappings/group")
    public GroupedMockMessages getGroupedMappingList() {
        final List<MockMessage> mappings = this.getMappingList();
        final List<MockMessagesGroup> groups = mappings
                .stream()
                .filter(MockMessage::hasGroup)
                .collect(Collectors.groupingBy(MockMessage::getGroup))
                .entrySet()
                .stream()
                .map(e -> new MockMessagesGroup(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        final List<MockMessage> noGroupMessages = mappings
                .stream()
                .filter(m -> !m.hasGroup())
                .collect(Collectors.toList());
        return new GroupedMockMessages(groups, noGroupMessages);
    }

    @ApiOperation(value = "MQ mapping getting single", notes = "Gets single mock by guid", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @GetMapping("mappings/{guid}")
    public MockMessage getMappingByGuid(@PathVariable String guid) {
        return mqRunnerComponent.getMappings().stream()
                .filter(m -> guid.equals(m.getGuid()))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    @ApiOperation(value = "MQ request list getting", notes = "Get request history", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @GetMapping("request-list")
    @ResponseBody
    public Collection getRequestList(@RequestParam(required = false, defaultValue = "${mq.requestBufferSize:1000}") Integer limit) {
        Buffer fifo = mqRunnerComponent.getFifo();
        List result = new LinkedList(fifo);
        if (limit != null && result.size() > limit) {
            result = result.subList(result.size() - limit, result.size());
        }
        return result;
    }

    @ApiOperation(value = "MQ mapping list clear", notes = "Clear request history", tags = "MqMock")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 500, message = "Internal Server Error")
            }
    )
    @PostMapping("request-list/clear")
    @ResponseBody
    public void clearRequestList() {
        mqRunnerComponent.getFifo().clear();
    }

    private <T> Function<T, String> valueOrDefault(Function<T, String> extractor) {
        return x -> {
            final String value = extractor.apply(x);
            return value != null ? value : StringUtils.EMPTY;
        };
    }
}
