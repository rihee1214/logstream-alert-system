FROM postgres:16.10-bookworm

# 기본 DB / USER(런타임에서 덮어쓸 수 있음)
ENV POSTGRES_DB=LogForDebugging \
    POSTGRES_USER=username

# 데이터 디렉토리를 볼륨으로 선언 (docker-compose에서 매핑하면 영속화)
VOLUME ["/var/lib/postgresql/data"]

EXPOSE 5432