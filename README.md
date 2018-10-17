# \[ 🚧 Work in progress 👷‍♀️⛏👷🔧️👷🔧 🚧 \] Plaid 2.0 

Rewriting Plaid using [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/), in Kotlin. 

🛑 During the initial development stage, we're not accepting external PRs. 🛑

👍 Comments and new issues created are welcomed. 

[![CircleCI](https://circleci.com/gh/nickbutcher/plaid/tree/master.svg?style=shield)](https://circleci.com/gh/nickbutcher/plaid/tree/master)

### Background

Plaid was written with one big goal: showcase material design in Android in a real application. While Plaid successfully achieved its goal, from an architecture point of view, it lacks all features that would make it a modular, scalable, testable and maintainable app: with UI logic in Android classes, no tests and only one module. 
Plaid represents a great real world app example: it provides a fairly complex set of functionalities, it has technical debt, it has features that have to be dealt with as APIs are being removed.
All of these problems are encountered by many projects in the Android community and therefore, make Plaid a suitable showcase for all the advantages that architecture components bring. 

#### Read more

* [Restitching Plaid - Updating Plaid to modern app standards](https://medium.com/@crafty/restitching-plaid-9ca5588d3b0a)
* [A patchwork Plaid - Monolith to modularized app](https://medium.com/androiddevelopers/a-patchwork-plaid-monolith-to-modularized-app-60235d9f212e)

### Goals
* Migrate Plaid to Architecture Components. The refactoring will follow the architecture described in [Guide to App Architecture](https://developer.android.com/jetpack/docs/guide).  
* Convert to Kotlin, while migrating to Architecture Components.
* Modularize the app using [dynamic feature modules](https://developer.android.com/guide/app-bundle/).
* Showcase the extensibility of the architecture by adding an extra data source, once the migration is finished.

### Non-Goals
Changes to the styles, themes, icons, animations, transitions or any other UI elements that were the initial focus of Plaid, are outside the scope of this refactoring. 

### Android Studio IDE setup

Plaid requires Android Studio version 3.2 or higher.

Plaid uses [ktlint](https://ktlint.github.io/) to enforce Kotlin coding styles.
Here's how to configure it for use with Android Studio (instructions adapted
from the ktlint [README](https://github.com/shyiko/ktlint/blob/master/README.md)):

- Close Android Studio if it's open
- Download ktlint:

  `curl -sSLO https://github.com/shyiko/ktlint/releases/download/0.27.0/ktlint && chmod a+x ktlint`

- Inside the project root directory run:

  `ktlint --apply-to-idea-project --android`

- Remove ktlint if desired:

  `rm ktlint`

- Start Android Studio

---

# Plaid 1.0

<img src="screenshots/plaid_demo.gif" width="300" align="right" hspace="20">

*Design news and inspiration.*

Plaid is a showcase of [material design](https://www.google.com/design/spec/) that we hope you will
keep installed. It pulls in news & inspiration from [Designer News](https://www.designernews.co/),
[Dribbble](https://dribbble.com/) & [Product Hunt](https://www.producthunt.com/). It demonstrates
the use of
[material principles](https://www.google.com/design/spec/material-design/introduction.html#introduction-principles)
to create tactile, bold, understandable UIs.

**[Install on Google Play (Beta Testing)](https://play.google.com/apps/testing/io.plaidapp)**


### Screenshots


<img src="screenshots/home_grid_framed.png" width="25%" />
<img src="screenshots/post_story_framed.png" width="25%" />
<img src="screenshots/dn_story_framed.png" width="25%" />
<img src="screenshots/dribbble_shot_framed.png" width="25%" />


##### Non-Goals
Plaid is a UI sample and seeks to demonstrate how to implement material design. To make this as clear as possible it explicitly does not attempt to:
* Provide opinionated **architecture/testing** advice; it utilizes vanilla Android components. For advice on this, I'd recommend [Blueprints](https://github.com/googlesamples/android-architecture).
* Support **pre-Lollipop** devices. Doing so is entirely possible, but complicates things. For advice on doing this, see [this fork](https://github.com/hzsweers/plaid/tree/z/moarbackport).


### License


```
Copyright 2015 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
