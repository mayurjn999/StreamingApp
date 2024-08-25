import { useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import "./App.css";
import VideoUpload from "./components/VideoUpload";
import { Toaster } from "react-hot-toast";

function App() {
  const [count, setCount] = useState(0);

  const [videoId, setVideoId] = useState(
    "e13ede00-7a26-41f0-86ed-ed94f9c5712b"
  );
  return (
    <>
      <Toaster />
      <div className="flex flex-col items-center space-y-9 justify-center py-9">
        <h1 className="text-2xl font-bold text-gray-700 dark:text-gray-100">
          Video Streaming App
        </h1>

        <div className="flex mt-14 w-full justify-around">
          <div>
            <h1 className="text-white">Playing Video</h1>
            <video
              style={{
                width: 500,
              }}
              src={`http://localhost:8080/api/v1/videos/stream/range/${videoId}`}
              controls
            ></video>

            <video
              id="my-video"
              class="video-js"
              controls
              preload="auto"
              width="640"
              height="264"
              data-setup="{}"
            >
               <source src={`http://localhost:8080/api/v1/videos/93da6009-fd03-4be6-8ba6-140eb7fe993d/master.m3u8`} type="application/x-mpegURL" />

              <p class="vjs-no-js">
                To view this video please enable JavaScript, and consider
                upgrading to a web browser that
                <a
                  href="https://videojs.com/html5-video-support/"
                  target="_blank"
                >
                  supports HTML5 video
                </a>
              </p>
            </video> 
          </div>

          <VideoUpload />
        </div>
      </div>
    </>
  );
}

export default App;
