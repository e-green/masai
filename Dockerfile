# Use this image as a base
FROM niaquinto/gradle
MAINTAINER "dewmal@egreen.io"
# In case someone loses the Dockerfile
RUN rm -rf /etc/Dockerfile
ADD Dockerfile /etc/Dockerfile
COPY . /usr/bin/app
# Set your default behavior
ENTRYPOINT ["gradle"]
CMD ["-PmainClass=io.egreen.newsmater.feedgrabber.Main", "launcher:execute"]