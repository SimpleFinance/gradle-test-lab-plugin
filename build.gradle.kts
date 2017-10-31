task("wrapper", type = Wrapper::class) {
    group = "build setup"
    gradleVersion = "4.3"
    distributionType = Wrapper.DistributionType.ALL
}
