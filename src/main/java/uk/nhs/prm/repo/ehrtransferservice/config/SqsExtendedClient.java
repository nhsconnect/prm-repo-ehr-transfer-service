package uk.nhs.prm.repo.ehrtransferservice.config;

//@Configuration
//@RequiredArgsConstructor
//public class SqsExtendedClient {
//    private final S3ClientSpringConfiguration s3;
//
//    @Value("${aws.sqsLargeMessageBucketName}")
//    private String bucketName;
//
//    @Bean
//    public AmazonSQSExtendedClient sqsExtendedClient(SqsClient sqsClient) {
//        var extendedClientConfiguration = new ExtendedClientConfiguration().withPayloadSupportEnabled(s3.s3Client(), bucketName, true);
//        return new AmazonSQSExtendedClient(sqsClient, extendedClientConfiguration);
//    }
//}
