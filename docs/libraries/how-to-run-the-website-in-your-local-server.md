# Arrow library: How to run the website in your local server

## Steps

### 1. Prepare the site

```
git clone https://github.com/arrow-kt/arrow-site.git
cd arrow-site
./gradlew runAnk
```

Then `build/site` directory will be created.

If you need to change any of the files on `arrow-site` (for instance, the sidebar menu), remember to do it before running `runAnk` task.

### 2. Copy the documentation from Arrow library

There are 4 main Arrow libraries that will provide the main pages to browse all the documentation:

* [Arrow Core](https://github.com/arrow-kt/arrow-core)
* [Arrow Fx](https://github.com/arrow-kt/arrow-fx)
* [Arrow Optics](https://github.com/arrow-kt/arrow-optics)
* [Arrow Incubator](https://github.com/arrow-kt/arrow-incubator)

Steps:

```
git clone https://github.com/arrow-kt/<arrow-library>.git
cd <arrow-library>
./gradlew buildArrowDoc
```

Then copy the result to the previous directory:

```
cp -r <arrow-library>/arrow-docs/build/site/* arrow-site/build/site/
```

### 3. Run the website in your local server

```bash
cd arrow-site
bundle install --gemfile Gemfile --path vendor/bundle
bundle exec jekyll serve -s build/site/
```

This will install any needed dependencies locally, and will use it to launch the complete website in [127.0.0.1:4000](http://127.0.0.1:4000) so you can open it with a standard browser.

If you get an error while installing the Ruby gem _http_parser_, check if the path to your Arrow directory contains spaces. According to this [issue](https://github.com/tmm1/http_parser.rb/issues/47), the installation with spaces in the path is currently not working.

### 4. Browse the documentation

Landing page might show different links for the main sections. However, they will be available in these links:

* Arrow Core: http://localhost:4000/core/
* Arrow Fx: http://localhost:4000/fx/
* Arrow Optics: http://localhost:4000/optics/dsl/
* Arrow Incubator: http://localhost:4000/aql/intro/

## How to test links

Test for broken links in documentation using

```sh
wget --spider -r -nd -nv -l 5 http://127.0.0.1:4000/docs/
```