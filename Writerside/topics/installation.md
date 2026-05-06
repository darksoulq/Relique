# Installation
<link-summary>Guide to adding Relique dependencies to your project and paper-plugin.yml</link-summary>

Integrating Relique into your plugin is straightforward and similar to using any other API. This guide will walk you through adding the necessary build dependencies and updating your plugin descriptor.

To get started, you will need to complete 2 steps:
1. Adding the Gradle or Maven Dependency to your build script.
2. Adding the `paper-plugin.yml` dependency so the server loads Relique first.


### Adding the Build Dependency
AbyssalLib artifacts are hosted on <a href="https://jitpack.io/#darksoulq/AbyssalLib">JitPack</a>.

<tabs>
<tab title="Gradle">
<code-block lang="Gradle">
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation('com.github.darksoulq:Relique:&lt;version&gt;')
}
</code-block>
</tab>
<tab title="Maven">
<code-block lang="xml">
&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;jitpack.io&lt;/id&gt;
        &lt;url&gt;https://jitpack.io&lt;/url&gt;
    &lt;/repository&gt;
&lt;/repositories&gt;
&lt;dependency&gt;
    &lt;groupId&gt;com.github.darksoulq&lt;/groupId&gt;
    &lt;artifactId&gt;Relique&lt;/artifactId&gt;
    &lt;version&gt;version&lt;/version&gt;
&lt;/dependency&gt;
</code-block>
</tab>
</tabs>

### Adding the paper-plugin.yml Dependency

Then just add it to your paper-plugin.yml (or plugin.yml) dependency.

```YAML
dependencies:
  server:
    Relique:
      required: true
      load: BEFORE
```