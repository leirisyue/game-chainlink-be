# Bước 1: Chọn hình ảnh Java 17 cho Dockerfile
FROM maven:3.8.3-openjdk-17

WORKDIR /usr/src/app

# Bước 2: Sao chép mã nguồn ứng dụng vào hình ảnh
COPY . /usr/src/app

# Bước 3: Sử dụng Maven để xây dựng ứng dụng
RUN mvn clean compile

# Bước 4: Thiết lập biến môi trường PORT và PROFILE cloud
ENV PORT 5000
ENV PROFILE cloud

EXPOSE $PORT

# Bước 5: Thiết lập lệnh CMD để chạy ứng dụng với Maven
ENTRYPOINT [ "sh", "-c", "mvn -Dspring-boot.run.arguments=--server.port=${PORT} -Dspring-boot.run.profiles=${PROFILE} spring-boot:run" ]
