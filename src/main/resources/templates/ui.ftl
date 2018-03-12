<html>

<head>
  <title>TEST UI PAGE</title>

  <link href="http://vjs.zencdn.net/6.6.3/video-js.css" rel="stylesheet"/>

  <!-- If you'd like to support IE8 -->
  <script src="http://vjs.zencdn.net/ie8/1.1.2/videojs-ie8.min.js"></script>

</head>

<body>

<!--
<video id="my-video" class="video-js" controls preload="auto" width="640" height="264"
       poster="MY_VIDEO_POSTER.jpg" data-setup="{}">
  <source src="MY_VIDEO.mp4" type='video/mp4'>
  <source src="MY_VIDEO.webm" type='video/webm'>
  <source src=".avi" type='video/webm'>

  <p class="vjs-no-js">
    To view this video please enable JavaScript, and consider upgrading to a web browser that
    <a href="http://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>
  </p>
</video>
-->

<script src="http://vjs.zencdn.net/6.6.3/video.js"></script>

<form action="/mock-upload" method="post" enctype="multipart/form-data">
  <div class="col s7 card center">
    <div class="card-content white-text">

      <div class="file-field input-field">
        <div class="btn-flat upload-button large">
          <span>Video</span>
          <input id="video-file" name="video" type="file"/>
        </div>
        <div class="file-path-wrapper">
          <span>Data</span>
          <input id="data-file" name="data" type="file"/>
        </div>
      </div>

    </div>
    <div class="card-action">
      <button type="submit"><i class="material-icons left">theaters</i> Upload Video</button>
    </div>
  </div>
</form>

</body>
</html>