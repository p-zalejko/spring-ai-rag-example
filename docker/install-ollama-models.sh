#docker-compose up

docker exec -it ollama ollama pull gpt-oss
docker exec -it ollama ollama pull nomic-embed-text

# docker exec -it ollama ollama pull qwen2.5-coder:14b
# docker exec -it ollama ollama pull qwen2.5-coder:7b
# docker exec -it ollama ollama pull qwen2.5-coder:3b
