import React, { useEffect, useState } from 'react';

const AndroidLogo: React.FC = () => {
  const [activeExperiment, setActiveExperiment] = useState<boolean>(false);

  useEffect(() => {
    const experiment = document.getElementById('root')?.dataset.experiment;
    console.log(experiment);
    setActiveExperiment(experiment === 'android');
  }, []);

  if (!activeExperiment) {
    return null;
  }

  return (
    activeExperiment && (
      <div className="android-logo">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -4 48 48" width="28px" height="28px">
          <path
            fill="#30dc80"
            d="M24,14.088C11.427,14.088,1.108,23.716,0,36h48C46.892,23.716,36.573,14.088,24,14.088z M33.179,27.079c0-1.104,0.895-1.999,1.999-1.999c1.104,0,1.999,0.895,1.999,1.999c0,1.104-0.895,1.999-1.999,1.999	C34.074,29.078,33.179,28.183,33.179,27.079z M12.822,29.078c-1.104,0-1.999-0.895-1.999-1.999c0-1.104,0.895-1.999,1.999-1.999	s1.999,0.895,1.999,1.999C14.821,28.183,13.926,29.078,12.822,29.078z"
          />
          <path
            fill="#30dc80"
            d="M34.038,19.313c-0.14,0-0.281-0.035-0.41-0.11c-0.393-0.227-0.527-0.729-0.301-1.122l5.197-9.008	c0.227-0.394,0.729-0.529,1.122-0.301c0.393,0.227,0.527,0.729,0.301,1.122l-5.197,9.008C34.598,19.166,34.322,19.313,34.038,19.313	z"
          />
          <path
            fill="#30dc80"
            d="M13.962,19.313c-0.284,0-0.56-0.148-0.712-0.411L8.054,9.894C7.827,9.501,7.962,8.999,8.354,8.772	c0.392-0.228,0.895-0.093,1.122,0.301l5.197,9.008c0.227,0.394,0.092,0.896-0.301,1.122C14.243,19.278,14.102,19.313,13.962,19.313z"
          />
        </svg>
      </div>
    )
  );
};

export default AndroidLogo;
