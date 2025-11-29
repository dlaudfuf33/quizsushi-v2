package com.cmdlee.quizsushi.ai.router;

import com.cmdlee.quizsushi.ai.adapter.AiModelAdapter;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiModelRouter {
    private final Map<String, AiModelAdapter> adapterMap;

    public AiModelAdapter getAdapter(String modelName) {

        AiModelAdapter adapter = adapterMap.get(modelName);
        if (adapter == null) {
            throw new GlobalException(ErrorCode.AI_MODEL_NOT_FOUND);
        }
        return adapter;
    }
}
