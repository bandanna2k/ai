```shell
curl -X POST http://localhost:8188/prompt \
  -H "Content-Type: application/json" \
  -d @prompt.json
```

Check queue status
```shell
curl http://localhost:8188/queue |jq
```

Check history (wait a bit, then run this)
```shell
curl http://localhost:8188/history | jq
```

Download the image
```shell
curl http://localhost:8188/view?filename=output_00001_.png --output /tmp/output_00001_.png
```
