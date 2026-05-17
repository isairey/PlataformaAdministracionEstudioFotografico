import logo from "../assets/icon-dark.svg";

type leFtLogoSideProps = {
    className?: string;
}
function LeftLogoSide({className: _className}: leFtLogoSideProps) {
    return (
    <div className='text-center lg:text-center flex flex-col items-center lg:items-start gap-6 px-4 lg:px-0'>
        <img src={logo} alt="agencja fotograficzna Logo" className="w-40 max-w-full" />
        <div className='text-2xl font-semibold'>
            Demo agencji fotograficznej
        </div>
        <div>
            Platforma przeznaczona dla członków agencji fotograficznej.
        </div>
    </div>
    )
}

export default LeftLogoSide;