package com.soaesps.documentsservice.controller;

import com.soaesps.documentsservice.DataModels.BaseDocument;
import com.soaesps.documentsservice.service.DocumentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/documents")
public class DocumentsController {
    @Autowired
    private DocumentsService ds;

    @GetMapping(value = "/documentId/{id}")
    public Mono<BaseDocument> getDocumentById(@PathVariable Long id) {
        return ds.findDocumentById(id);
    }

    @ResponseBody
    @GetMapping(value = "/docsByExample")
    Flux<BaseDocument> findAllDocs(@RequestBody BaseDocument doc) {
        return ds.findAllByExample(doc);
    }

    @PostMapping(value = "/creation")
    public Mono<BaseDocument> createDoc(@RequestBody BaseDocument doc) {
        return ds.save(doc);
    }

    @PostMapping(value = "/creations")
    public Flux<BaseDocument> createDocs(@RequestBody Flux<BaseDocument> docs) {
        return docs.flatMap(ds::save);
    }

    @DeleteMapping(value = "/removing/{id}")
    public Mono<String> deleteDoc(@PathVariable Long id) {
        return ds.deleteById(id)
                .map(p -> ServerResponse.ok().toString());
    }
}