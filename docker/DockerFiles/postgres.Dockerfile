FROM postgres:16.10-bookworm

# 기본 DB / USER(런타임에서 덮어쓸 수 있음)
ENV POSTGRES_DB=LogForDebugging \
    POSTGRES_USER=username

# init 스크립트 복사 (첫 실행 시에만 자동 실행됨)
# 고민해야하는 부분, sql 어덯게 실행할지 등등
#COPY initdb/ /docker-entrypoint-initdb.d/

# 데이터 디렉토리를 볼륨으로 선언 (docker-compose에서 매핑하면 영속화)
VOLUME ["/var/lib/postgresql/data"]

EXPOSE 5432
# 공식 엔트리포인트와 CMD를 그대로 사용 (이미 이미지에 포함)